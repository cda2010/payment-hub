(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ModalController', ModalController);

    ModalController.$inject = ['$timeout','$scope','$stateParams','$state','$uibModal','$uibModalInstance','entity'];

    function ModalController($timeout,$scope,$state,$stateParams,$uibModal,$uibModalInstance,entity) {
        var vm = this;
        $scope.obj = entity;
            console.log('modal',entity);
         $scope.fermerM= function () {
             $uibModalInstance.dismiss('cancel');
         }

    }
})();
