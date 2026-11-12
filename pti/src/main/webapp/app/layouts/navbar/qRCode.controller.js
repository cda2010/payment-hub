(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('QrcodeModalController', QrcodeModalController);

    QrcodeModalController.$inject = ['$http','Principal','User','$scope','$stateParams','$state','$uibModal','$uibModalInstance','entity'];

    function QrcodeModalController($http,Principal,User,$scope,$state,$stateParams,$uibModal,$uibModalInstance,entity) {
        var vm = this;
        $scope.obj = entity;
        vm.data='test';
        //var temp=null;
        // var typeNumber = 4;
        // var errorCorrectionLevel = 'L';
        // var qr = qrcode(typeNumber, errorCorrectionLevel);
        // var test='data';
        // qr.addData('Hi!');
        // qr.make();
        // document.getElementById('test').innerHTML = qr.createImgTag(5,10);

        Principal.identity().then(function (account) {
            vm.account = account;
            if (account != null) {
                startVerif();
                console.log(account.login);
            }
        });

        function startVerif() {
            var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
            $http.jsonp(url)
                .success(function (data, status, headers, config) {
                    data.login = angular.copy(vm.username);
                  //  vm.sendToLogin(JSON.stringify(data), vm.username);
                    //$sessionStorage.infos=JSON.stringify(data);
                    console.log(JSON.stringify(data));
                    vm.data=JSON.stringify(data);
                    User.getTempPass({data:vm.data},function (response) {
                        console.log('temp',response.tempPass);
                        $scope.temp=response.tempPass;
                    });
                }).error(function (data, status, headers, config) {
                Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
            });
        }
        $scope.fermerM= function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.validerOp= function () {
            $uibModalInstance.close();
        }

    }
})();
