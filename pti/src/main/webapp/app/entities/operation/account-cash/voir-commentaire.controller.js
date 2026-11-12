(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VoirCommentaireController', VoirCommentaireController);


    VoirCommentaireController.$inject = ['$scope','$stateParams','$state','$uibModal','$uibModalInstance','Notification','entity','Regulation','Utils','OperationUtils','EtiquetteCompte'];

    function VoirCommentaireController($scope,$state,$stateParams,$uibModal,$uibModalInstance,Notification,entity,Regulation,Utils,OperationUtils,EtiquetteCompte) {

        var vm = this;
        vm.commentaire = entity;
        vm.clear=clear;
        console.log('etq',vm.commentaire);



        $scope.fermerM= function () {
            $uibModalInstance.dismiss('cancel');
        };

        function clear() {
            console.log("clear");
            $uibModalInstance.dismiss('cancel');
        }

    }
})();
