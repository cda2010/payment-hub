(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneFichierController', LigneFichierController);

    LigneFichierController.$inject = ['$state', '$scope','LigneFichier', 'AlertService', '$timeout','Upload','Notification','CancelAlert','Principal'];

    function LigneFichierController($state,$scope, LigneFichier,  AlertService, $timeout,Upload,Notification,CancelAlert,Principal) {

        var vm = this;

    }
})();