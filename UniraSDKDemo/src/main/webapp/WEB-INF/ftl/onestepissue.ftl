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
<h1>NetONEX UniRA Online Test2</h1>

<form action="" id="iform" method="post">
    <input type="button" value="Issue" action="issue" api="csp" ajax="${base}/onestepissue"
           />
    <input type="hidden" name="ou" value="深圳市"/>
</form>


<div action="netonex" netonexid="netonex" activex32_codebase="${base}/activex/NetONEX32.v1.4.8.5.cab"
     activex64_codebase="${base}/activex/NetONEX64.v1.4.8.5.cab" msi_codebase="${base}/activex/NetONEX.v1.4.8.5.msi"
     version="1.4.8.5" logshowid="divlog">
    <object width="1" height="1" classid="CLSID:EC336339-69E2-411A-8DE3-7FF7798F8307"
            codebase="${base}/activex/NetONEX32.v1.4.8.5.cab#Version=1,4,8,5"></object>
</div>
<div id="divlog">
</div>


</body>
</html>