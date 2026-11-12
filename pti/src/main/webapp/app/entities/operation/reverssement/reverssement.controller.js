(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReverssementController', ReverssementController);


    ReverssementController.$inject = ['$scope','$stateParams','$state','$uibModal','Notification','entity','Regulation','Utils','RequestUtils'];

    function ReverssementController($scope,$state,$stateParams,$uibModal,Notification,entity,Regulation,Utils,RequestUtils) {

        var vm = this;
        vm.clear=clear;




    }
})();
