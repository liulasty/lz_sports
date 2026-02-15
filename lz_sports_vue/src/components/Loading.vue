<template>
    <div class="loading-screen">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在加载，请稍候...</div>
    </div>
  </template>
  
  <style>
  .loading-screen {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .loading-spinner {
    /* 添加你喜欢的加载动画样式 */
    background-color: greenyellow;
  }
  
  .loading-text {
    color: white;
    margin-top: 10px;
  }
  </style>
  