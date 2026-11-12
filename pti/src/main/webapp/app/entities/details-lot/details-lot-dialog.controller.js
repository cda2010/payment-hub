/* eslint-disable no-console */
/* eslint-disable angular/log */
(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailsLotDialogController', DetailsLotDialogController);

        DetailsLotDialogController.$inject = ['$timeout', '$scope', 'DataUtils','$stateParams', '$uibModalInstance', 'entity', 'Lot', 'UniteOrganisation'];

    function DetailsLotDialogController ($timeout, $scope, DataUtils, $stateParams, $uibModalInstance, entity, Lot, UniteOrganisation) {
        var vm = this;

        vm.lot = entity;
        vm.clear = clear;
        vm.save = save;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        // vm.lots = Facture.query();
        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        // console.log(vm.lot.organisation.id);

        function save () {
            console.log(vm.lot);
            // vm.facturier.ftpDTO=vm.ftpDTO;
           // vm.facturier.recepteurEmailDTO=vm.recepteurEmailDTO;
          //  vm.facturier.fse='0013';
            
            vm.isSaving = true;
            if (vm.lot.id !== null) {
                Lot.update(vm.lot, onSaveSuccess, onSaveError);
            } else {
                Lot.save(vm.lot, onSaveSuccess, onSaveError);
                
            }
        }

        function onSaveSuccess (result) {
            //$scope.$emit('ptiApp:facturierUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        // vm.setBordereau = function setBordereau($file,operation) {
        //     console.log($file);
        //     if ($file && $file.$error === 'pattern') {
        //         return;
        //     }
        //     if ($file) {
        //         DataUtils.toBase64($file, function (base64Data) {
        //             $scope.$apply(function () {
        //                 //console.log("chargement d'une image "+type);
        //                 operation.valeur = base64Data;
        //                 operation.valeurContentType = $file.type;

        //             });
        //         });
        //     }
        // };

    }
})();
