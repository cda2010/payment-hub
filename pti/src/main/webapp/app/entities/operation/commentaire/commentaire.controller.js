(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommentaireController', CommentaireController);


    CommentaireController.$inject = ['$scope','$stateParams','$state','$uibModal','$uibModalInstance','Notification','entity','Regulation','Utils','OperationUtils','EtiquetteCompte'];

    function CommentaireController($scope,$state,$stateParams,$uibModal,$uibModalInstance,Notification,entity,Regulation,Utils,OperationUtils,EtiquetteCompte) {

        var vm = this;
        vm.operation = entity;
        vm.commentaire=null;
        vm.clear=clear;
        console.log('etq',vm.commentaire);



        $scope.fermerM= function () {
            $uibModalInstance.dismiss('cancel');
        };

        function clear() {
            console.log("clear");
            $uibModalInstance.dismiss('cancel');
        }

        vm.saveCommentaire= function () {
            vm.operation.commentaire=vm.operation.commentaire+"\n"+"Checker : "+vm.commentaire;
            console.log('---------',vm.operation.commentaire);
            $uibModalInstance.dismiss('cancel');
        }

    }
})();
