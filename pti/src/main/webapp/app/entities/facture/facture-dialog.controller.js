(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FactureDialogController', FactureDialogController);

    FactureDialogController.$inject = ['$timeout', '$scope', '$state', '$uibModalInstance', 'DateUtils','entity', 'Facture', 'Produit'];

    function FactureDialogController ($timeout, $scope, $state, $uibModalInstance,DateUtils ,entity, Facture, Produit) {
        var vm = this;

        vm.facture = entity;
        vm.clear = clear;
        vm.save = save;
        vm.nombre=1;
        vm.produits = Produit.query();
        vm.disableMontant=false;
        vm.setMontant=setMontant;
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};

        if(vm.facture!=null){
            console.log(vm.facture);
            vm.facture.datePaiement=new Date(vm.facture.datePaiement);
        }
        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        function setMontant(idProduit){
            Produit.get({id : idProduit},function (result){
                //console.log(result);
                if(vm.disableMontant=result.aUnMontant){
                    vm.facture.montantFacture=result.montant;
                }else {
                    vm.facture.montantFacture=null;
                }
            } )
        }

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            if(vm.facture.datePaiement!=null){
                console.log(vm.facture.datePaiement.toString());
                var dateString=DateUtils.convertLocalDateToServer(vm.facture.datePaiement);
                vm.facture.datePaiement=dateString;
                console.log(vm.facture.datePaiement,'et',dateString);
            }
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
                        produit:vm.facture.produit,
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
