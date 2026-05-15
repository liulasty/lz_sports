package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.dto.EligibilityConfigDTO;
import com.lz.dto.EligibilityPreviewVO;
import com.lz.eligibility.engine.*;
import com.lz.eligibility.resolver.AthleteContextResolver;
import com.lz.mapper.EligibilityConfigMapper;
import com.lz.mapper.EligibilityGroupMapper;
import com.lz.mapper.EligibilityRuleMapper;
import com.lz.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {

    private static final String OWNER_TYPE_EVENT_ITEM = "EVENT_ITEM";

    private final EligibilityConfigMapper configMapper;
    private final EligibilityGroupMapper groupMapper;
    private final EligibilityRuleMapper ruleMapper;
    private final AthleteContextResolver contextResolver;
    private final RuleEvaluator ruleEvaluator = new RuleEvaluator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public EligibilityResult check(Long userId, Long eventId, Long itemId) {
        AthleteContext context = contextResolver.resolve(userId, eventId);
        if (context == null) {
            return EligibilityResult.fail("运动员信息不存在");
        }
        return checkInternal(context, itemId);
    }

    @Override
    public EligibilityResult checkByAthlete(Long athleteUserId, Long eventId, Long itemId) {
        return check(athleteUserId, eventId, itemId);
    }

    @Override
    public List<EligibilityPreviewVO> batchCheck(Long userId, Long eventId, List<Long> itemIds) {
        AthleteContext context = contextResolver.resolve(userId, eventId);
        if (context == null || itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }

        // batch load configs
        List<com.lz.entity.EligibilityConfig> configs = configMapper.selectList(
                new LambdaQueryWrapper<com.lz.entity.EligibilityConfig>()
                        .eq(com.lz.entity.EligibilityConfig::getOwnerType, OWNER_TYPE_EVENT_ITEM)
                        .in(com.lz.entity.EligibilityConfig::getOwnerId, itemIds));

        Map<Long, com.lz.entity.EligibilityConfig> configMap = configs.stream()
                .collect(Collectors.toMap(com.lz.entity.EligibilityConfig::getOwnerId, c -> c, (a, b) -> a));

        Set<Long> configIds = configs.stream().map(com.lz.entity.EligibilityConfig::getId).collect(Collectors.toSet());
        List<com.lz.entity.EligibilityGroup> groupEntities = configIds.isEmpty() ? List.of()
                : groupMapper.selectList(new LambdaQueryWrapper<com.lz.entity.EligibilityGroup>()
                        .in(com.lz.entity.EligibilityGroup::getConfigId, configIds));
        Map<Long, List<com.lz.entity.EligibilityGroup>> groupsByConfigId = groupEntities.stream()
                .collect(Collectors.groupingBy(com.lz.entity.EligibilityGroup::getConfigId));

        Set<Long> groupIds = groupEntities.stream().map(com.lz.entity.EligibilityGroup::getId).collect(Collectors.toSet());
        List<com.lz.entity.EligibilityRule> ruleEntities = groupIds.isEmpty() ? List.of()
                : ruleMapper.selectList(new LambdaQueryWrapper<com.lz.entity.EligibilityRule>()
                        .in(com.lz.entity.EligibilityRule::getGroupId, groupIds));
        Map<Long, List<com.lz.entity.EligibilityRule>> rulesByGroupId = ruleEntities.stream()
                .collect(Collectors.groupingBy(com.lz.entity.EligibilityRule::getGroupId));

        List<EligibilityPreviewVO> results = new ArrayList<>();
        for (Long itemId : itemIds) {
            com.lz.entity.EligibilityConfig config = configMap.get(itemId);
            if (config == null || Boolean.FALSE.equals(config.getEnabled())) {
                results.add(new EligibilityPreviewVO(itemId, true, null));
                continue;
            }

            List<GroupEntry> engineGroups = buildEngineGroups(
                    groupsByConfigId.getOrDefault(config.getId(), List.of()), rulesByGroupId);
            GroupLogic combination = parseGroupLogic(config.getGroupCombination());
            EligibilityResult result = ruleEvaluator.evaluate(context, engineGroups, combination);
            results.add(new EligibilityPreviewVO(itemId, result.isPassed(), result.getReason()));
        }
        return results;
    }

    @Override
    public EligibilityConfigDTO getConfig(Long itemId) {
        com.lz.entity.EligibilityConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<com.lz.entity.EligibilityConfig>()
                        .eq(com.lz.entity.EligibilityConfig::getOwnerType, OWNER_TYPE_EVENT_ITEM)
                        .eq(com.lz.entity.EligibilityConfig::getOwnerId, itemId));
        if (config == null) return null;

        List<com.lz.entity.EligibilityGroup> groupEntities = groupMapper.selectList(
                new LambdaQueryWrapper<com.lz.entity.EligibilityGroup>()
                        .eq(com.lz.entity.EligibilityGroup::getConfigId, config.getId())
                        .orderByAsc(com.lz.entity.EligibilityGroup::getSortOrder));

        List<com.lz.entity.EligibilityRule> ruleEntities;
        if (!groupEntities.isEmpty()) {
            Set<Long> groupIds = groupEntities.stream().map(com.lz.entity.EligibilityGroup::getId).collect(Collectors.toSet());
            ruleEntities = ruleMapper.selectList(new LambdaQueryWrapper<com.lz.entity.EligibilityRule>()
                    .in(com.lz.entity.EligibilityRule::getGroupId, groupIds)
                    .orderByAsc(com.lz.entity.EligibilityRule::getSortOrder));
        } else {
            ruleEntities = List.of();
        }

        Map<Long, List<com.lz.entity.EligibilityRule>> rulesByGroupId = ruleEntities.stream()
                .collect(Collectors.groupingBy(com.lz.entity.EligibilityRule::getGroupId));

        EligibilityConfigDTO dto = new EligibilityConfigDTO();
        dto.setGroupCombination(config.getGroupCombination());
        dto.setEnabled(config.getEnabled());

        List<EligibilityConfigDTO.GroupDTO> groupDTOs = new ArrayList<>();
        for (com.lz.entity.EligibilityGroup g : groupEntities) {
            EligibilityConfigDTO.GroupDTO gdto = new EligibilityConfigDTO.GroupDTO();
            gdto.setGroupLogic(g.getGroupLogic());
            gdto.setGroupDesc(g.getGroupDesc());

            List<com.lz.entity.EligibilityRule> rules = rulesByGroupId.getOrDefault(g.getId(), List.of());
            List<EligibilityConfigDTO.RuleDTO> ruleDTOs = rules.stream().map(r -> {
                EligibilityConfigDTO.RuleDTO rdto = new EligibilityConfigDTO.RuleDTO();
                rdto.setDimension(r.getDimension());
                rdto.setOperator(r.getOperator());
                rdto.setValue(parseJsonValue(r.getValueJson()));
                return rdto;
            }).collect(Collectors.toList());
            gdto.setRules(ruleDTOs);
            groupDTOs.add(gdto);
        }
        dto.setGroups(groupDTOs);
        return dto;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveConfig(Long itemId, EligibilityConfigDTO dto) {
        deleteConfigInternal(itemId);

        if (dto == null || dto.getGroups() == null || dto.getGroups().isEmpty()) {
            return;
        }

        com.lz.entity.EligibilityConfig config = new com.lz.entity.EligibilityConfig();
        config.setOwnerType(OWNER_TYPE_EVENT_ITEM);
        config.setOwnerId(itemId);
        config.setGroupCombination(dto.getGroupCombination() != null ? dto.getGroupCombination() : "AND");
        config.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        config.initTime();
        configMapper.insert(config);

        int groupOrder = 0;
        for (EligibilityConfigDTO.GroupDTO gdto : dto.getGroups()) {
            com.lz.entity.EligibilityGroup group = new com.lz.entity.EligibilityGroup();
            group.setConfigId(config.getId());
            group.setGroupLogic(gdto.getGroupLogic() != null ? gdto.getGroupLogic() : "AND");
            group.setGroupDesc(gdto.getGroupDesc());
            group.setSortOrder(groupOrder++);
            group.initTime();
            groupMapper.insert(group);

            int ruleOrder = 0;
            if (gdto.getRules() != null) {
                for (EligibilityConfigDTO.RuleDTO rdto : gdto.getRules()) {
                    com.lz.entity.EligibilityRule rule = new com.lz.entity.EligibilityRule();
                    rule.setGroupId(group.getId());
                    rule.setDimension(rdto.getDimension());
                    rule.setOperator(rdto.getOperator());
                    rule.setValueJson(toJsonString(rdto.getValue()));
                    rule.setSortOrder(ruleOrder++);
                    rule.initTime();
                    ruleMapper.insert(rule);
                }
            }
        }

        log.info("资格规则已保存: itemId={}, groups={}", itemId, dto.getGroups().size());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deleteConfig(Long itemId) {
        deleteConfigInternal(itemId);
        log.info("资格规则已清空: itemId={}", itemId);
    }

    // ---- internal ----

    private EligibilityResult checkInternal(AthleteContext context, Long itemId) {
        com.lz.entity.EligibilityConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<com.lz.entity.EligibilityConfig>()
                        .eq(com.lz.entity.EligibilityConfig::getOwnerType, OWNER_TYPE_EVENT_ITEM)
                        .eq(com.lz.entity.EligibilityConfig::getOwnerId, itemId));
        if (config == null || Boolean.FALSE.equals(config.getEnabled())) {
            return EligibilityResult.ok();
        }

        List<com.lz.entity.EligibilityGroup> groupEntities = groupMapper.selectList(
                new LambdaQueryWrapper<com.lz.entity.EligibilityGroup>()
                        .eq(com.lz.entity.EligibilityGroup::getConfigId, config.getId())
                        .orderByAsc(com.lz.entity.EligibilityGroup::getSortOrder));
        if (groupEntities.isEmpty()) return EligibilityResult.ok();

        List<Long> groupIds = groupEntities.stream().map(com.lz.entity.EligibilityGroup::getId).collect(Collectors.toList());
        List<com.lz.entity.EligibilityRule> ruleEntities = ruleMapper.selectList(
                new LambdaQueryWrapper<com.lz.entity.EligibilityRule>()
                        .in(com.lz.entity.EligibilityRule::getGroupId, groupIds)
                        .orderByAsc(com.lz.entity.EligibilityRule::getSortOrder));

        Map<Long, List<com.lz.entity.EligibilityRule>> rulesByGroupId = ruleEntities.stream()
                .collect(Collectors.groupingBy(com.lz.entity.EligibilityRule::getGroupId));

        List<GroupEntry> engineGroups = buildEngineGroups(groupEntities, rulesByGroupId);
        return ruleEvaluator.evaluate(context, engineGroups, parseGroupLogic(config.getGroupCombination()));
    }

    private List<GroupEntry> buildEngineGroups(
            List<com.lz.entity.EligibilityGroup> groupEntities,
            Map<Long, List<com.lz.entity.EligibilityRule>> rulesByGroupId) {

        List<GroupEntry> result = new ArrayList<>();
        for (com.lz.entity.EligibilityGroup g : groupEntities) {
            List<com.lz.entity.EligibilityRule> rules = rulesByGroupId.getOrDefault(g.getId(), List.of());
            List<RuleEntry> engineRules = rules.stream().map(r ->
                    new RuleEntry(parseDimension(r.getDimension()), parseOperator(r.getOperator()), parseJsonValue(r.getValueJson()))
            ).collect(Collectors.toList());

            result.add(new GroupEntry(parseGroupLogic(g.getGroupLogic()), g.getGroupDesc(), engineRules));
        }
        return result;
    }

    private void deleteConfigInternal(Long itemId) {
        com.lz.entity.EligibilityConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<com.lz.entity.EligibilityConfig>()
                        .eq(com.lz.entity.EligibilityConfig::getOwnerType, OWNER_TYPE_EVENT_ITEM)
                        .eq(com.lz.entity.EligibilityConfig::getOwnerId, itemId));
        if (config == null) return;

        List<com.lz.entity.EligibilityGroup> groups = groupMapper.selectList(
                new LambdaQueryWrapper<com.lz.entity.EligibilityGroup>()
                        .eq(com.lz.entity.EligibilityGroup::getConfigId, config.getId()));
        if (!groups.isEmpty()) {
            List<Long> groupIds = groups.stream().map(com.lz.entity.EligibilityGroup::getId).collect(Collectors.toList());
            ruleMapper.delete(new LambdaQueryWrapper<com.lz.entity.EligibilityRule>()
                    .in(com.lz.entity.EligibilityRule::getGroupId, groupIds));
            groupMapper.delete(new LambdaQueryWrapper<com.lz.entity.EligibilityGroup>()
                    .eq(com.lz.entity.EligibilityGroup::getConfigId, config.getId()));
        }
        configMapper.deleteById(config.getId());
    }

    // ---- parsing ----

    private static Dimension parseDimension(String s) {
        try { return Dimension.valueOf(s); } catch (Exception e) { return Dimension.GENDER; }
    }

    private static Operator parseOperator(String s) {
        try { return Operator.valueOf(s); } catch (Exception e) { return Operator.EQ; }
    }

    private static GroupLogic parseGroupLogic(String s) {
        try { return GroupLogic.valueOf(s.toUpperCase()); } catch (Exception e) { return GroupLogic.AND; }
    }

    private Object parseJsonValue(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                return objectMapper.readValue(trimmed, String.class);
            }
            if (trimmed.startsWith("[")) {
                return objectMapper.readValue(trimmed, new TypeReference<List<Object>>() {});
            }
            if (trimmed.startsWith("{")) {
                return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
            }
            if (!trimmed.isEmpty() && (trimmed.charAt(0) == '-' || Character.isDigit(trimmed.charAt(0)))) {
                return objectMapper.readValue(trimmed, Integer.class);
            }
            return objectMapper.readValue(trimmed, Object.class);
        } catch (Exception e) {
            log.warn("Failed to parse eligibility rule value_json: {}", json, e);
            return json;
        }
    }

    private String toJsonString(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize rule value to JSON", e);
            return "\"" + value + "\"";
        }
    }
}
