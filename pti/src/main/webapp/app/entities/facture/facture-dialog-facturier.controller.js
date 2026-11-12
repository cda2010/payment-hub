(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FactureDialogFacturierController', FactureDialogFacturierController);

    FactureDialogFacturierController.$inject = ['$timeout', '$scope', '$state', '$uibModalInstance', 'entity', 'Facture', 'Produit'];

    function FactureDialogFacturierController ($timeout, $scope, $state, $uibModalInstance, entity, Facture, Produit) {
        var vm = this;
        vm.facture = entity;
        vm.clear = clear;
        vm.save = save;
        vm.nombre=1;
        vm.produits = entity.produits;
        console.log('---Show-----',vm.facture);
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.facture.id !== null) {
                Facture.update(vm.facture, onSaveSuccess, onSaveError);
            } else {
                var i=0;
                while (i<vm.nombre){
                    var facture={
                        referenceFacature: vm.facture.referenceFacature,
                        montantFacture: vm.facture.montantFacture,
                        referencePaiement: null,
                        datePaiement: null,
                        id: null,
                        produit:vm.facture.produit
                    };
                    Facture.save(facture, onSaveSuccess, onSaveError);
                    i++;
                }
               // $state.go("facture");
                //$uibModalInstance.close(true);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:factureUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
