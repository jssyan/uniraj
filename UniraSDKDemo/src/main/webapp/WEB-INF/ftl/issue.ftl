<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <link href="${base}/themes/default/gui.css" rel="stylesheet" type="text/css"/>

    <script type="text/javascript" src="${base}/js/jquery.js"></script>
    <script type="text/javascript" src="${base}/js/jquery.sprintf.js"></script>
    <script type="text/javascript" src="${base}/js/objectclass.js"></script>

    <script type="text/javascript" src="${base}/js/syan.activex.src.js"></script>
    <script type="text/javascript" src="${base}/js/netonex.base.src.js"></script>
    <script type="text/javascript" src="${base}/js/netonex.enroll.src.js"></script>
    <style>
        html, body {
            font-family: 'Microsoft YaHei', Tahoma, Verdana, Arial, Helvetica, sans-serif;
            font-size: 12px;
            /*background: #DDD url('images/background.gif');*/
        }

        .red {
            color: #CC0000;
            font-weight: bold;
        }

        .green {
            color: #0C5F1F;
            font-weight: bold;
        }

        .yellow {
            color: #998300;
            font-weight: bold;
        }
    </style>

</head>
<body>
<h1>NetONEX UniRA Online</h1>

<form action="" id="iform" method="post">
    用户 <strong><span>${userName!}</span></strong> 注册并审核成功! 提取码(Client ID): <span>${client_id!}</span><br/><br/><br/>
    <input type="button" value="Issue" action="issue" api="skf" ajax="${base}/issue"
           />
           <#--tokenname="Microsoft Enhanced Cryptographic Provider v1.0"/>-->
        <input type="button" value="冻结" action="freeze" clientid="${client_id!}" api="skf" ajax="${base}/freeze"/>
        <input type="button" value="解冻" action="unfreeze" clientid="${client_id!}" api="skf" ajax="${base}/unfreeze"/>
        <input type="button" value="废除" action="revoke" clientid="${client_id!}" api="skf" ajax="${base}/revoke"/>
        <input type="button" value="更新" action="renew" clientid="${client_id!}" api="skf" ajax="${base}/renew"/>
</form>
<div action="netonex" netonexid="netonex" activex32_codebase="${base}/activex/NetONEX32.v1.4.8.5.cab"
     activex64_codebase="${base}/activex/NetONEX64.v1.4.8.5.cab" msi_codebase="${base}/activex/NetONEX.v1.4.8.5.msi"
     version="1.4.8.5" logshowid="divlog">
    <object width="1" height="1" classid="CLSID:EC336339-69E2-411A-8DE3-7FF7798F8307"
            codebase="${base}/activex/NetONEX32.v1.4.8.5.cab#Version=1,4,8,5"></object>
</div>
<div id="divlog">
</div>

<div>
    UniRA的在线签发基本模式有3个参与者: 用户页面, 代理(基于UniXX SDK), UniRA Online服务
    <ul>
        <li>用户页面负责页面展示, 它只和代理通信;</li>
        <li>代理负责数据中转, 它和用户页面以及后台UniRA Online服务通信;</li>
        <li>UniRA Online服务提供签发所需数据;</li>
    </ul>

    因此:
    <ul>
        <li>用户页面需要设置:
            <ul>
                <li>代理服务的URL, 即上面Issue按钮中的ajax属性, 这个属性应该指向代理的服务URL;</li>
                <li>设置api, 例如:csp, 或者skf</li>
                <li>设置证书token, 在本例中, 是直接签发证书到微软的CSP(Microsoft Enhanced Cryptographic Provider v1.0). 如果不设置,
                    则将弹出token选择框, 让用户选择
                </li>
            </ul>
        </li>
        <li>代理需要设置:
            <ul>
                <li>UniRA Online的服务URL</li>
                <li>操作员证书及私钥</li>
                <li>本示例中, 代理也由本页面来模拟</li>
            </ul>
        </li>
    </ul>

    发证流程如下:
    <ol>
        <li>点击Issue按钮, 触发javascript中的issue函数</li>
        <li>issue函数通过ajax post方法发送页面信息到代理, 其action标识为pkcs10, 表示是要获取生成密钥对的相关信息</li>
        <li>代理获取ajax数据, 判断是action=pkcs10, 发送gencsr命令到UniRA Online服务</li>
        <li>UniRA Online返回gencsr相关数据到代理, 代理组织成json编码的数据, 返回给javascript</li>
        <li>页面javascript调用createPKCS10函数, 根据代理的返回指令, 生成密钥对, 并把公钥通过csr返回给代理, 其action标识为install, 表示提交csr并等待安装证书</li>
        <li>代理获取ajax数据, 判断是action=install, 发送install命令到UniRA Online</li>
        <li>UniRA Online获取代理数据, 签发证书, 并返回给代理</li>
        <li>代理返回证书信息到页面</li>
        <li>页面中的javascript调用install方法, 导入新签发的证书</li>
    </ol>

    冻结流程如下:
    <ol>
        <li>点击"冻结"按钮, 触发javascript中的freeze函数</li>
        <li>freeze函数通过ajax post方法发送页面信息到代理, 其action标识为freeze, 表示是要冻结该证书</li>
        <li>代理获取ajax数据, 判断是action=freeze, 调用UniRA Online的/crt/freeze接口冻结该证书</li>
        <li>UniRA Online返回response数据给javascript</li>
    </ol>

    解冻流程如下:
    <ol>
        <li>点击"解冻"按钮, 触发javascript中的unfreeze函数</li>
        <li>unfreeze函数通过ajax post方法发送页面信息到代理, 其action标识为unfreeze, 表示是要解冻该证书</li>
        <li>代理获取ajax数据, 判断是action=unfreeze, 调用UniRA Online的/crt/unfreeze接口解冻该证书</li>
        <li>UniRA Online返回response数据给javascript</li>
    </ol>

    废除流程如下:
    <ol>
        <li>点击"废除"按钮, 触发javascript中的revoke函数</li>
        <li>revoke函数通过ajax post方法发送页面信息到代理, 其action标识为revoke, 表示是要废除该证书</li>
        <li>代理获取ajax数据, 判断是action=revoke, 调用UniRA Online的/crt/revoke接口废除该证书</li>
        <li>UniRA Online返回response数据给javascript</li>
    </ol>

    更新流程如下:
    <ol>
        <li>点击"更新"按钮, 触发javascript中的renew函数</li>
        <li>renew函数通过ajax post方法发送页面信息到代理, 其action标识为renew, 表示是要更新该证书</li>
        <li>代理获取ajax数据, 判断是action=renew, 调用UniRA Online的/crt/renew接口更新该证书</li>
        <li>UniRA Online返回response数据给javascript</li>
    </ol>

    <span class="red">* 冻结、解冻、废除和更新操作必须在发证步骤之后才可操作</span>
</div>
</body>
</html>