/* eslint-disable no-console */
/* eslint-disable angular/log */
(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FacturierDialogController', FacturierDialogController);

    FacturierDialogController.$inject = ['$timeout', '$scope', 'DataUtils','$stateParams', '$uibModalInstance', 'entity', 'Facturier', 'Facture','UniteOrganisation','ClassificationFacturier','FournisseurService'];

    function FacturierDialogController ($timeout, $scope, DataUtils,$stateParams, $uibModalInstance, entity, Facturier, Facture,UniteOrganisation,ClassificationFacturier,FournisseurService,RecepteurEmail,Ftp) {
        var vm = this;

        vm.facturier = entity;
        vm.clear = clear;
        vm.save = save;
        vm.initClassifications=initClassifications;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
       // vm.factures = Facture.query();
        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
        vm.MARCHANDs=UniteOrganisation.queryByTypeUoMARCHAND();
        vm.fournisseurs=FournisseurService.find();
        vm.classifications=[];

        vm.recepteurEmailDTO={
            id: null,
            email: null
        };

        vm.ftpDTO={
            id: null,
            url: null,
            username: null,
            password: null,
            repertoire: null
        };

        if(vm.facturier.id){
          //  initClassifications(vm.facturier.posseseur.id);
        }
        if(vm.facturier.possesseur){
            initClassifications(vm.facturier.possesseur.id);
        }

        function initClassifications(idPossesseur){
            console.log("rechere des classifications de l'uo d'id : "+idPossesseur);
            vm.classifications = ClassificationFacturier.queryFinalByUo({id:idPossesseur});
        }

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            console.log('facturier',vm.facturier);
            console.log('facturier',vm.facturier.classification.id);
            // vm.facturier.ftpDTO=vm.ftpDTO;
           // vm.facturier.recepteurEmailDTO=vm.recepteurEmailDTO;
          //  vm.facturier.fse='0013';
            
            if(vm.facturier.typeFacturier==="AUTRES"){
                vm.facturier.uniteOrganisation=null;
            }
            vm.isSaving = true;
            if (vm.facturier.id !== null) {
                Facturier.update(vm.facturier, onSaveSuccess, onSaveError);
            } else {
                Facturier.save(vm.facturier, onSaveSuccess, onSaveError);
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

        vm.setBordereau = function setBordereau($file,operation) {
            console.log($file);
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function (base64Data) {
                    $scope.$apply(function () {
                        //console.log("chargement d'une image "+type);
                        operation.valeur = base64Data;
                        operation.valeurContentType = $file.type;

                    });
                });
            }
        };

    }
})();
