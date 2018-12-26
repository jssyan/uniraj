// using version of bind from prototype for scoping purposes

var EnrollX = NetONEX.extend({
    AJAX_URL: "",
    CryptoInterface: 1,
    FORMID: 'iform',
    PreferredToken: "",

    getEnrollX: function () {
        var r = (this.CryptoInterface == 2) ? this.getCSPEnrollX() : this.getSKFEnrollX();
        return r;
    },

    getSigninX: function () {
        var r = this.getCertificateCollectionX();
        return r;
    },

    makeQuery: function (q) {
        var f = $('#' + this.FORMID);
        var r = $.sprintf('%s&api=%s&%s', q, (this.CryptoInterface == 2) ? 'csp' : 'skf', f.serialize());
        return r;
    },

    checkResponse: function (resp, action) {
        //alert(dump(resp));
        if (!resp) {
            document.body.style.cursor = "default";
            this.log(new Error('Invalid response. operation failed. (' + action + ')'));
            return false;
        }

        if (resp.status) {
            document.body.style.cursor = "default";
            this.log(new Error(resp.error));
            return false;
        }
        return true;
    },

    selectToken: function () {
        var ex = this.getEnrollX();
        var t;
        if (this.PreferredToken) {
            t = ex.GetToken(this.PreferredToken);
            if (!t) {
                throw new Error($.sprintf('Token (%s) does not exists.', this.PreferredToken));
            }
        }
        else {
            t = ex.SelectTokenDialog();
            if (!t) {
                throw new Error('Operation canceled by user.');
            }
        }
        ex.TokenName = t.Name;
        if (ex.CryptoInterface == 1) {
            ex.ProviderModuleName = t.ProviderModuleName;
        }
        this.log($.sprintf('Using %s', t.ToString()));
        //ex.ApplicationName = 'cstore';
    },

    getTokenInfo: function () {
        var ex = this.getEnrollX();
        var r = {};
        if (ex.CryptoInterface == 1) {
            var t = ex.GetToken(ex.TokenName);
            if (!t) {
                throw new Error($.sprintf('Token does not exists. (%s)', ex.TokenName));
            }
            r.manufacturer = t.Manufacturer;
            if (!r.manufacturer) {
                throw new Error($.sprintf('Retrieve token information failed. (%s)', ex.TokenName));
            }
            r.serial = t.SerialNumber;
            r.hwversion = t.HWVersion;
            r.fwversion = t.FirmwareVersion;
        }
        else {
            r.manufacturer = ex.TokenName;
        }
        return r;
    },

    issue: function () {
        document.body.style.cursor = "wait";
        try {
            this.selectToken();
            var ajaxReq = this.makeQuery("action=pkcs10");
            this.log('Creating PKCS#10 certificate request...');
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (data) {
                    self.createPKCS10(data);
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    renew: function (clientid) {
        document.body.style.cursor = "wait";
        try {
            this.selectToken();
            var ex = this.getEnrollX();
            ex.ContainerName = clientid;
            ex.UserPIN = '';
            var c = ex.GetCertificate(1);
            if (!c || !('Content' in c)) {
                throw new Error($.sprintf('Can not find required certificate in %s.', ex.Path));
            }
            var ajaxReq = "action=renew&crt=" + encodeURIComponent(c.Content);
            ajaxReq = this.makeQuery(ajaxReq);
            this.log('Preparing for certificate renew ...');
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (resp) {
                    try {
                        if (self.checkResponse(resp, 'renew')) {
                            var i = ex.RenewCertificate(self.asString(resp.certificatea), self.asString(resp.certificatee));
                            if (i != 0) {
                                throw new Error("Renew certficate failed. e: 0x" + i.toString(16));
                            }
                            else {
                                self.log('Renew certficate succeed.');
                            }
                        }
                    }
                    catch (e) {
                        self.log(e);
                    }
                    document.body.style.cursor = "default";
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    freeze: function(clientid){
        document.body.style.cursor = "wait";
        try {
            this.selectToken();
            var ex = this.getEnrollX();
            ex.ContainerName = clientid;
            ex.UserPIN = '';
            var c = ex.GetCertificate(1);
            if (!c || !('Content' in c)) {
                throw new Error($.sprintf('Can not find required certificate in %s.', ex.Path));
            }
            var ajaxReq = "action=freeze&crt=" + encodeURIComponent(c.Content);
            ajaxReq = this.makeQuery(ajaxReq);
            this.log('Preparing for certificate freeze ...');
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (resp) {
                    try {
                        if (self.checkResponse(resp, 'freeze')) {
                            self.log('Freeze certficate succeed.');
                        }
                    }
                    catch (e) {
                        self.log(e);
                    }
                    document.body.style.cursor = "default";
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    unfreeze: function(clientid){
        document.body.style.cursor = "wait";
        try {
            this.selectToken();
            var ex = this.getEnrollX();
            ex.ContainerName = clientid;
            ex.UserPIN = '';
            var c = ex.GetCertificate(1);
            if (!c || !('Content' in c)) {
                throw new Error($.sprintf('Can not find required certificate in %s.', ex.Path));
            }
            var ajaxReq = "action=unfreeze&crt=" + encodeURIComponent(c.Content);
            ajaxReq = this.makeQuery(ajaxReq);
            this.log('Preparing for certificate unfreeze ...');
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (resp) {
                    try {
                        if (self.checkResponse(resp, 'unfreeze')) {
                            self.log('Unfreeze certificate succeed.');
                        }
                    }
                    catch (e) {
                        self.log(e);
                    }
                    document.body.style.cursor = "default";
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    revoke: function(clientid){
        document.body.style.cursor = "wait";
        try {
            this.selectToken();
            var ex = this.getEnrollX();
            ex.ContainerName = clientid;
            ex.UserPIN = '';
            var c = ex.GetCertificate(1);
            if (!c || !('Content' in c)) {
                throw new Error($.sprintf('Can not find required certificate in %s.', ex.Path));
            }
            var ajaxReq = "action=revoke&crt=" + encodeURIComponent(c.Content);
            ajaxReq = this.makeQuery(ajaxReq);
            this.log('Preparing for certificate revoke ...');
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (resp) {
                    try {
                        if (self.checkResponse(resp, 'revoke')) {
                            self.log('Revoke certificate succeed.');
                        }
                    }
                    catch (e) {
                        self.log(e);
                    }
                    document.body.style.cursor = "default";
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    recover: function (clientid) {
        document.body.style.cursor = "wait";
        try {
            this.selectToken();
            var ajaxReq = this.makeQuery("action=pkcs10&clientid=" + clientid);
            this.log('Creating PKCS#10 certificate request...');
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (data) {
                    self.createPKCS10(data);
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    createPKCS10: function (resp) {
        if (!this.checkResponse(resp, 'pkcs10')) {
            return;
        }
        try {
            var tokeninfo = this.getTokenInfo();
            var ex = this.getEnrollX();
            ex.ContainerName = resp.containername;
            if (this.CryptoInterface == 1) {
                ex.DevAuthKey = resp.devauthkey;
            }
            ex.UserPIN = resp.userpin;
            this.log($.sprintf('%s, Key: %s-%d, %s', ex.Path, this.asInt(resp.keytype), this.asInt(resp.keybits), resp.uniqid));
            if (!ex.KeyExists(1)) {
                this.log('Generating key pair ....');
                var i = ex.GenerateKeyPair(this.asInt(resp.keytype), this.asInt(resp.keybits));
                if (i != 0) {
                    throw new Error("Generate key pair failed. e: 0x" + i.toString(16));
                }
            }
            this.log('Creating certificate request ...');
            var i = ex.GenerateCSR(resp.uniqid);
            if (i != 0) {
                throw new Error("Generate certificate request failed. e: 0x" + i.toString(16));
            }
            this.log('Start certificate issue...');
            //this.log(ex.NewCSR);
            //this.log(this.AJAX_URL);
            var ajaxReq = $.sprintf("action=install&csr=%s&tokeninfo=%s", encodeURIComponent(ex.NewCSR), encodeURIComponent(JSON.stringify(tokeninfo)));
            ajaxReq = this.makeQuery(ajaxReq);
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (data) {
                    //alert(data);
                    self.install(data);
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
    },

    /*
     batchIssue:function() {
     this.BATCH = true;
     this.issue();
     },

     stepBatchCount: function() {
     var eleObj = document.getElementById(this.ELEMENTID_COUNT);
     if (eleObj) {
     eleObj.innerHTML = parseInt(eleObj.innerHTML) + 1;
     }
     },

     setClientInformation: function(resp) {
     $('#'+this.ELEMENTID_CLIENTINFO).html(resp.nextclientinformation);
     },

     batchNext: function(resp) {
     this.stepBatchCount();
     if ((resp.nextclientid == false) || (typeof resp.nextclientid == "undefined")) {
     this.log('All certificates have been issued.');
     return;
     }
     this.setClientInformation(resp);
     if (false == confirm("Continue?\nPlease replace the token and press [OK] when ready. Press [Cancel] will abort current operation.")) {
     this.log('Operation canceled by user.');
     return;
     }
     this.logClean();
     this.issue();
     },
     */

    install: function (resp) {
        if (!this.checkResponse(resp, 'install')) {
            return;
        }
        try {
            var ex = this.getEnrollX();
            this.log('Installing ...');
            this.log($.sprintf('%s', ex.Path));
            var i = ex.Install(this.asString(resp.certificatea), this.asString(resp.ekeyblob), this.asString(resp.certificatee));

            if (i) {
                //var h = 0xffffffff + i + 1;
                throw new Error("Certificate issue failed. e: 0x" + i.toString(16));
            }
            this.log('Certificate issue finished sucessfully.');
            //if (this.BATCH) {
            //	this.batchNext(resp);
            //}
        }
        catch (e) {
            this.log(e);
        }
    },

    check: function () {
        this.logClean();
        document.body.style.cursor = "wait";
        try {
            var ex = this.getEnrollX();
            var cc = ex.GetCertificateCollection(ex.TokenName, 1);
            if (cc.Size == 0) {
                throw new Error($.sprintf('No certificate is found in [%s]', ex.TokenName ? ex.TokenName : '*'));
            }

            var r = {};
            var i;

            for (i = 0; i < cc.Size; i++) {
                var p = cc.GetAt(i);
                if (!('Content' in p)) {
                    continue;
                }
                t = {};
                t.crt = p.Content;
                t.token = p.TokenName;
                t.application = p.ApplicationName;
                t.container = p.ContainerName;
                r[i] = t;
            }

            var ajaxReq = 'action=check&checkdata=' + encodeURIComponent(JSON.stringify(r));
            ajaxReq = this.makeQuery(ajaxReq);
            var self = this;
            $.post(this.AJAX_URL,
                ajaxReq,
                function (data) {
                    if (self.checkResponse(data, 'check')) {
                        self.log(data.html);
                    }
                    document.body.style.cursor = "default";
                },
                'json');
        }
        catch (e) {
            this.log(e);
        }
        document.body.style.cursor = "default";
    },

    signin: function (cookie, tagid, thumb) {
        try {
            var colx = this.getSigninX();
            colx.CryptoInterface = 3;
            colx.Load();
            var c;
            if (thumb) {
                c = colx.Find(thumb);
                if (!c) {
                    throw new Error("Can not find this certificate.");
                }
            }
            else {
                c = colx.SelectCertificateDialog();
                if (!c) {
                    throw new Error("Operation aborted.");
                }
            }
            var sig = c.PKCS1String(cookie);
            if (!sig) {
                throw new Error("Create signature failed.");
            }
            $('#login_' + tagid + '_log').html('<span class="green">OK</span>');
            $('#login_' + tagid + '_sig').val(sig);
            $('#login_' + tagid + '_crt').val(c.Content);
            return true;
        }
        catch (e) {
            $('#login_' + tagid + '_log').html('<span class="red">N/A</span>');
            $('#login_' + tagid + '_sig').val('');
            $('#login_' + tagid + '_crt').val('');
            this.log(e);
            return false;
        }
    },

    importcrt: function (dataid, viewid) {
        try {
            var colx = this.getSigninX();
            colx.CryptoInterface = 3;
            colx.Load();
            var c = colx.SelectCertificateDialog();
            if (!c) {
                throw new Error("Operation aborted.");
            }
            $('#' + dataid).val(c.Content);
            $('#' + viewid).html(this.showCertificate(c));
        }
        catch (e) {
            return false;
        }
    },

    uploadcrt: function (dataid, viewid) {
        try {
            var colx = this.getSigninX();
            var c = colx.CreateCertificateFile('');
            if (!c) {
                throw new Error("Operation aborted.");
            }
            $('#' + dataid).val(c.Content);
            $('#' + viewid).html(this.showCertificate(c));
        }
        catch (e) {
            return false;
        }
    },

    setupButton: function () {
        var self = this;
        $('input:button[action="issue"]').off();
        $('input:button[action="issue"]').on('click', function () {
            self.PreferredToken = $(this).attr('tokenname');
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            self.issue();
        });
        $('input:button[action="renew"]').off();
        $('input:button[action="renew"]').on('click', function () {
            self.PreferredToken = $(this).attr('tokenname');
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            var c = $(this).attr('clientid');
            self.renew(c);
        });
        $('input:button[action="freeze"]').off();
        $('input:button[action="freeze"]').on('click', function () {
            self.PreferredToken = $(this).attr('tokenname');
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            var c = $(this).attr('clientid');
            self.freeze(c);
        });
        $('input:button[action="unfreeze"]').off();
        $('input:button[action="unfreeze"]').on('click', function () {
            self.PreferredToken = $(this).attr('tokenname');
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            var c = $(this).attr('clientid');
            self.unfreeze(c);
        });
        $('input:button[action="revoke"]').off();
        $('input:button[action="revoke"]').on('click', function () {
            self.PreferredToken = $(this).attr('tokenname');
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            var c = $(this).attr('clientid');
            self.revoke(c);
        });
        $('input:button[action="recover"]').off();
        $('input:button[action="recover"]').on('click', function () {
            self.PreferredToken = $(this).attr('tokenname');
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            var c = $(this).attr('clientid');
            self.recover(c);
        });
        $('input:button[action="check"]').off();
        $('input:button[action="check"]').on('click', function () {
            var u = $(this).attr('ajax');
            self.AJAX_URL = u;
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            self.check();
        });
        $('input:button[action="signin"]').off();
        $('input:button[action="signin"]').on('click', function () {
            var c = $(this).attr('cookie');
            var n = $(this).attr('tagid');
            var t = $(this).attr('thumb');
            var s = $(this).attr('submit');
            self.CryptoInterface = 1;
            if (self.signin(c, n, t) && s) {
                $(this).closest('form').append('<input type="hidden" name="__LOGIN" value="1" />');
                if (t) {
                    $(this).closest('form').append($.sprintf('<input type="hidden" name="__thumb[%d]" value="%s" />', n, t));
                }
                $(this).closest('form').submit();
            }
        });
        $('input:button[action="logclean"]').off();
        $('input:button[action="logclean"]').on('click', function () {
            var a = $(this).attr('api');
            self.CryptoInterface = (a == 'csp') ? 2 : 1;
            self.logClean();
        });
        $('input:button[action="importcrt"]').off();
        $('input:button[action="importcrt"]').on('click', function () {
            var a = $(this).attr('dataid');
            var v = $(this).attr('viewid');
            self.importcrt(a, v);
        });
        $('input:button[action="uploadcrt"]').off();
        $('input:button[action="uploadcrt"]').on('click', function () {
            var a = $(this).attr('dataid');
            var v = $(this).attr('viewid');
            self.uploadcrt(a, v);
        });
    },

    showCertificate: function (cx) {
        var x;
        x = $('<table />');
        x.append($.sprintf('<tr><td class="vncellreq">%s</td><td class="listlr">%s</td></tr>', 'Friendly Name', cx.FriendlyName));
        x.append($.sprintf('<tr><td class="vncellreq">%s</td><td class="listlr">%s</td></tr>', 'Issuer', cx.Issuer));
        x.append($.sprintf('<tr><td class="vncellreq">%s</td><td class="listlr">%s</td></tr>', 'Subject', cx.Subject));
        x.append($.sprintf('<tr><td class="vncellreq">%s</td><td class="listlr">%s</td></tr>', 'Serial', cx.SerialNumberHex));
        x.append($.sprintf('<tr><td class="vncellreq">%s</td><td class="listlr">%s (%d)</td></tr>', 'Algorithm', cx.Algorithm, cx.Keybits));
        return $.sprintf('<table style="width: 100%; border: 1px solid #999999;" cellpadding="3" cellspacing="0">%s</table>', x.html());
    },

    showSignInButton: function (cx, cookie, tagid, submit) {
        var r;
        var o = $.sprintf('type="button" class="formbtn" value="Sign In" action="signin" cookie="%s" tagid="%s"', cookie, tagid);
        if (cx) {
            o += $.sprintf(' thumb="%s"', cx.ThumbprintSHA1);
        }
        if (submit) {
            o += ' submit="yes"';
        }
        r = $.sprintf('<input %s />', o);
        return r;
    },

    setupSignIn: function () {
        var self = this;
        $('div[action="listcrt"]').each(function (index) {
            var colx = self.getSigninX();
            colx.CryptoInterface = 3;
            var n = colx.Load();

            var cookie = $(this).attr('cookie');
            var tagid = $(this).attr('tagid');
            var submit = $(this).attr('submit');
            var thumb = $(this).attr('thumb');

            var ul = $(this).append('<ul class="bjqs">').find('ul');
            var startidx = 0;
            for (var i = 0; i < n; i++) {
                var cx = colx.GetAt(i);
                if (thumb == cx.ThumbprintSHA1) {
                    startidx = i + 1;
                }
                var li = $($.sprintf('<li>%s<div align="center">%s</div></li>', self.showCertificate(cx), self.showSignInButton(cx, cookie, tagid, submit)));
                ul.append(li);
            }

            var w = $(this).width();
            $(this).bjqs({
                animtype: 'slide',
                automatic: false,
                height: 320,
                width: w,
                responsive: true,
                nexttext: ">>",
                prevtext: "<<",
                ctrltopoffset: 0.8,
                startindex: startidx
            });
        });
    }
});

$(document).ready(function () {
    var objX = new EnrollX();
    try {
        objX.setupObject();
        objX.setupSignIn();
        objX.setupButton();
    }
    catch (e) {
        objX.log(e);
        alert(e);
    }
});
