(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RegleCommissionDialogController', RegleCommissionDialogController);

    RegleCommissionDialogController.$inject = ['DateUtils','EtiquetteCompte','$timeout', '$scope', 'RequestUtils', '$uibModalInstance', 'entity', 'RegleCommission', 'TypeService', 'UniteOrganisation'];

    function RegleCommissionDialogController (DateUtils,EtiquetteCompte,$timeout, $scope, RequestUtils, $uibModalInstance, entity, RegleCommission, TypeService, UniteOrganisation) {
        var vm = this;

        vm.regleCommission = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.typeservices = RequestUtils.ServicesFrais.query();
        RequestUtils.GetOrganisationByType.get({ type: "FSE" }, function(datas) {
            vm.uniteorganisations= datas;
        });

        RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function(datas) {
            vm.pays= datas;
        });


        if (vm.regleCommission.id != null) {
            vm.regleCommission.dateDebut = DateUtils.convertLocalDateFromServer(vm.regleCommission.dateDebut);
            vm.regleCommission.dateFin = DateUtils.convertLocalDateFromServer(vm.regleCommission.dateFin);
        }

        vm.etiquetteComptes =RequestUtils.EtiquetteCompteList.query();
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if(vm.regleCommission.typeOperateur==""){
                vm.regleCommission.typeOperateur=null;
            }
             if(vm.regleCommission.min==""){
                vm.regleCommission.min=null;
            }
            if(vm.regleCommission.max==""){
                vm.regleCommission.max=null;
            }
            if (vm.regleCommission.id !== null) {
                RegleCommission.update(vm.regleCommission, onSaveSuccess, onSaveError);
            } else {
                RegleCommission.save(vm.regleCommission, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:regleCommissionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDebut = false;
        vm.datePickerOpenStatus.dateFin = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
