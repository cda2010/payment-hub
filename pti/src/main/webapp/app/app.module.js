(function () {
    'use strict';

    angular
        .module('ptiApp', [
            'ngStorage',
            'tmh.dynamicLocale',
            'pascalprecht.translate',
            'ngResource',
            'ngCookies',
            'ngAria',
            'ngCacheBuster',
            'ngFileUpload',
            'ui.bootstrap',
            'ui.bootstrap.datetimepicker',
            'ui.router',
            'toaster',
            'ngAnimate',
            'infinite-scroll',
            // jhipster-needle-angularjs-add-module JHipster will add new module here
            'angular-loading-bar',
            'ui-notification',
            'chart.js',
            'toaster',
            'ntt.TreeDnD',
            'dynform',
            'ngMask',
            'ui.select',
            'purplefox.numeric',
            'simple-accordion',
            'monospaced.qrcode'
        ])
        .constant('_', window._) // Ajout de la librairie lodash
        .run(run);

    run.$inject = ['stateHandler', 'translationHandler', 'Notification', '$rootScope'];

    function run(stateHandler, translationHandler, Notification, $rootScope) {
        stateHandler.initialize();
        translationHandler.initialize();
        $rootScope.$on('ptiApp.httpError', function (event, err) {
            if (err && err.data && err.status !== 500 && err.status !== 404) {
                if ((err.data.error_description != "" && err.data.error_description != null) || (err.data.message != null && err.data.message != "")) {
                    if (err.data.error == "invalid_grant") {
                        Notification.error("Identifiants invalides! ");
                    } else if (err.data.error_description != "Not Found" && err.data.error_description != "" && err.data.error_description)
                        Notification.error(err.data.error_description);
                    else if (err.data.message != null && err.data.message != "") {
                        Notification.error(err.data.message);
                    }
                }
            }
        });
    }
})();
