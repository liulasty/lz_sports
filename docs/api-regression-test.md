# API Regression Test Cases (Phase 1-3)

## Overview
This document outlines the API test cases for verifying the functionality of Phase 1 (User/Auth), Phase 2 (Event/Core), and Phase 3 (Score/Notification).

## Phase 1: User & Authentication

| ID | Test Case | Method | Endpoint | Description | Expected Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1.1** | **User Login** | `POST` | `/api/auth/login` | Login with valid credentials (admin/student). | `200 OK`, returns Token & User Info. |
| **1.2** | **Send Code** | `POST` | `/api/auth/send-code` | Send verification code to email. | `200 OK`, "验证码已发送". |
| **1.3** | **Verify Code** | `POST` | `/api/auth/verify-code` | Verify email code. | `200 OK`, returns verifyToken. |
| **1.4** | **User Register** | `POST` | `/api/auth/register` | Register new user with verifyToken. | `200 OK`, "注册成功". |
| **1.5** | **Get User Info** | `GET` | `/api/user/info` | Get current user profile. | `200 OK`, returns user details. |
| **1.6** | **Update Password** | `PUT` | `/api/user/password` | Update user password. | `200 OK`, "密码修改成功". |

## Phase 2: Event Core & Registration

| ID | Test Case | Method | Endpoint | Description | Expected Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **2.1** | **Create Event** | `POST` | `/api/event/EventList` | Admin creates a new event. | `200 OK`, "发布成功". |
| **2.2** | **List Events** | `GET` | `/api/event/page` | List events with pagination. | `200 OK`, returns event list. |
| **2.3** | **Update Event Status** | `PUT` | `/api/event/{id}/status` | Change event status (DRAFT -> OPEN). | `200 OK`, "操作成功". |
| **2.4** | **Create Project** | `POST` | `/api/project` | Add a project to an event. | `200 OK`, "添加成功". |
| **2.5** | **List Projects** | `GET` | `/api/project/event/{eventId}` | List projects for a specific event. | `200 OK`, returns project list. |
| **2.6** | **Apply Athlete** | `POST` | `/api/athlete` | User applies for athlete qualification. | `200 OK`, "申请提交成功". |
| **2.7** | **Audit Athlete** | `PUT` | `/api/athlete/audit/{id}` | Admin approves athlete application. | `200 OK`, "审核通过". |
| **2.8** | **Register Project** | `POST` | `/api/registration` | Athlete registers for a project. | `200 OK`, "报名成功". |
| **2.9** | **My Registrations** | `GET` | `/api/registration/my` | Get current user's registrations. | `200 OK`, returns list. |
| **2.10** | **Audit Registration** | `PUT` | `/api/registration/audit` | Admin audits registration (batch). | `200 OK`, "操作成功". |

## Phase 3: Score & Notification

| ID | Test Case | Method | Endpoint | Description | Expected Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **3.1** | **Import Scores** | `POST` | `/api/score/import/{eventId}` | Admin imports scores via Excel. | `200 OK`, returns success/fail count. |
| **3.2** | **List Scores (Admin)** | `GET` | `/api/score/page` | Admin views all scores (including drafts). | `200 OK`, returns score list. |
| **3.3** | **Publish Scores** | `PUT` | `/api/score/publish/{eventId}` | Admin publishes scores for event. | `200 OK`, "发布成功". |
| **3.4** | **List Scores (Public)** | `GET` | `/api/score/page` | Public/Athlete views published scores. | `200 OK`, returns published scores only. |
| **3.5** | **Export Registration** | `GET` | `/api/registration/export/{eventId}` | Admin exports registration list. | `200 OK`, returns Excel file. |
| **3.6** | **Get Notifications** | `GET` | `/api/notification/page` | Get user notifications. | `200 OK`, returns list. |
| **3.7** | **Unread Count** | `GET` | `/api/notification/unread-count` | Get unread notification count. | `200 OK`, returns count integer. |
| **3.8** | **Mark Read** | `PUT` | `/api/notification/read/{id}` | Mark single notification as read. | `200 OK`, "操作成功". |
| **3.9** | **Mark All Read** | `PUT` | `/api/notification/read-all` | Mark all notifications as read. | `200 OK`, "操作成功". |

## Execution Guide

1.  **Environment**: Ensure Backend is running on `http://localhost:8080`.
2.  **Tool**: Use IntelliJ HTTP Client, Postman, or VS Code REST Client.
3.  **Auth**: Most endpoints require `Authorization: Bearer <token>` header.
    *   Login first to get the token.
    *   Replace `{{auth_token}}` in your requests.

