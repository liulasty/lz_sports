import 'jquery';
import 'layui';
import 'layui/dist/css/layui.css';



export default function initLayui() {
    layui.config({
        dir: 'layui/dist/layui.js' //layui.js 所在目录（如果是 script 单独引入 layui.js，无需设定该参数）一般可无视
        ,version: true //一般用于更新模块缓存，默认不开启。设为 true 即让浏览器不缓存。也可以设为一个固定的值，如：201610
        ,debug: false //用于开启调试模式，默认 false，如果设为 true，则JS模块的节点会保留在页面
        ,base: '' //设定扩展的 layui 模块的所在目录，一般用于外部模块扩展
      });
    layui.define(['transfer','rate', 'carousel'], function (exports) {
        var rate = layui.rate;

        // 初始化评分组件
        rate.render({
            elem: '#score', // 绑定元素的选择器
            length: 5, // 评分的总星数
            value: 3, // 默认选中的星数
            readonly: false, // 是否只读
            theme: '#FF5722', // 主题颜色
            text: true, // 是否显示评分文字
            setText: function (value) { // 自定义评分文字的回调函数
                this.span.text(value + "星");
            }
        });
        var carousel = layui.carousel;
        // 这里可以使用 rate 和 carousel 组件进行相关操作
        //建造实例
        carousel.render({
            elem: '#test1'
            ,height: '332px'
            , width: '500px' //设置容器宽度
            , arrow: 'hover' //始终显示箭头
            ,anim: 'fade' //切换动画方式
            ,indicator: 'outside'
            ,interval: 3000,//切换速度
        });

        //触发轮播切换事件
        carousel.on('change(test1)', function (obj) { //test1来源于对应HTML容器的 lay-filter="test1" 属性值
            console.log(obj.index); //当前条目的索引
            console.log(obj.prevIndex); //上一个条目的索引
            console.log(obj.item); //当前条目的元素对象
        });

        //穿梭框
        var transfer = layui.transfer;
   
        //渲染
        transfer.render({
          elem: '#test2'  //绑定元素
          ,data: [
            {"value": "1", "title": "李白", "disabled": "", "checked": ""}
            ,{"value": "2", "title": "杜甫", "disabled": "", "checked": ""}
            ,{"value": "3", "title": "贤心", "disabled": "", "checked": ""}
          ]
          ,id: 'demo1' //定义索引
        });

        exports('index', {});
    });
}
