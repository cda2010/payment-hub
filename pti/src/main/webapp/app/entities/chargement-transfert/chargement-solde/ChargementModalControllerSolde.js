(function() {
    'use strict';
    angular
        .module('ptiApp')
        .controller('ChargementModalSoldeController', ChargementModalSoldeController);


    ChargementModalSoldeController.$inject = ['$scope','$stateParams','$state','$uibModal','$uibModalInstance','Notification','entity','ChargementTransfert'];

    function ChargementModalSoldeController($scope,$state,$stateParams,$uibModal,$uibModalInstance,Notification,entity,ChargementTransfert) {

        var vm = this;
        vm.ligneChargement=entity;
        console.log(vm.ligneChargement);
        console.log(vm.ligneChargement.chargementTransfert.codePays);
        vm.codePays=vm.ligneChargement.chargementTransfert.codePays;
        ChargementTransfert.getCompteOfLigneChg({valeurCompte:vm.ligneChargement.numeroTelephone,codePays:vm.codePays}, function (reponse) {
            console.log('result',reponse);
            vm.compte = reponse;
            vm.soldeAvant=vm.ligneChargement.soldeAvant;
            console.log('compte',vm.compte.compteAutre);

        });
        $scope.fermerM= function () {
            $uibModalInstance.dismiss('cancel');
        };

    }
})();
