<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <link href="${base}/themes/default/gui.css" rel="stylesheet" type="text/css"/>
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
<h1>注册并审核示例</h1>

<form action="${base}/process " method="post">

    <table class="tabcont" width="800" border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td class="vncellreq"><label>CN:</label></td>
            <td class="vtable"><input type="text" name="name" value="RSA测试1"><br>
                <span class="vexpl">请输入姓名.</span>
            </td>
        </tr>
        <tr>
            <td class="vncellreq"><label>OU:</label></td>
            <td class="vtable"><input type="text" name="ou" value="安徽CA"><br>
                <span class="vexpl">请输入组织机构.</span>
            </td>
        </tr>
        <tr>
            <td class="vncellreq"><label>C:</label></td>
            <td class="vtable"><input type="text" name="c" value="CN" disabled><br>
                <span class="vexpl">中国</span>
            </td>
        </tr>
        <tr>
            <td class="vncellreq"></td>
            <td class="vtable"><br><input type="submit" value="提交注册并审核"/><br></td>
        </tr>
    </table>


</form>

<br>
<br>
<div>
    本页表单中输入的数据和CA的证书模板中证书主题项一致，并可根据项目需要灵活修改
    <ul>
        <li>本页提交的信息包含两个动作：1）注册信息到Unira中；2）审核该用户通过，进入到待签发证书的状态</li>
        <li>本Demo在UniraSDKDemoController中默认设置有效期为1年，实际应用中可根据需要设置证书有效期</li>
        <li>Unira Cloud 的管理登录链接为：<a href="http://www.syan.com.cn:2115/unira/cloud/index" target="_blank">Unira Cloud </a></li>
        <li>Unira 管理登录链接为：<a href="http://www.syan.com.cn:2115/unira/mngr/login" target="_blank">Unira 管理 </a> 提供RA管理、应用管理、审计等功能，</li>
        <li>如果使用WIN10操作系统IE浏览器测试发证，请使用管理员身份打开IE</li>
    </ul>
</div>
</body>
</html>