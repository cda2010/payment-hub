(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauFraisDialogController', TableauFraisDialogController);

    TableauFraisDialogController.$inject = ['DateUtils','UniteOrganisationByType', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TableauFrais', 'LigneFrais', 'TypeService', 'Canal', 'TypeUniteOrganisation', 'Client', 'UniteOrganisation'];

    function TableauFraisDialogController(DateUtils,UniteOrganisationByType, $timeout, $scope, $stateParams, $uibModalInstance, entity, TableauFrais, LigneFrais, TypeService, Canal, TypeUniteOrganisation, Client, UniteOrganisation) {
        var vm = this;

        vm.tableauFrais = entity;
        vm.clear = clear;
        vm.save = save;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.lignefrais = LigneFrais.query();
        vm.typeservices = TypeService.queryAll();
        vm.canals = Canal.query();
        TypeUniteOrganisation.query({}, function(datas) {
            vm.typeuniteorganisations = datas;
            _.remove(vm.typeuniteorganisations, function(type) {
                return type.libelleTypeUO == "UNITE_REGROUPEMENTss";
            });
        });
       // vm.clients = Client.query();
        //vm.uniteorganisations = UniteOrganisation.query();
        if (vm.tableauFrais.id != null) {
            vm.tableauFrais.dateDebut = DateUtils.convertLocalDateFromServer(vm.tableauFrais.dateDebut);
            vm.tableauFrais.dateFin = DateUtils.convertLocalDateFromServer(vm.tableauFrais.dateFin);
        }
        vm.organisationColums = [{
            name: 'libelleUniteOrganisation',
            title: 'Libelle'
        }];
        vm.typeUOChange = function typeUOChange(typeId, type) {
            UniteOrganisationByType.queryOptimise({ typeId: typeId }, function(datas) {
                if (type == "EM") {
                    vm.uniteorganisationsEm = datas;
                } else {
                    vm.uniteorganisationsAcq = datas;
                }
            }, function() {});
        }
        if (vm.tableauFrais.id != null) {
            vm.typeUOChange(vm.tableauFrais.typeUoAcquereurId, 'ACQ');
            vm.typeUOChange(vm.tableauFrais.typeUoEmetteurId, 'EM');
        }

        $timeout(function() {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            console.log("clear");
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            console.log("saving");
            vm.isSaving = true;
            if (vm.tableauFrais.id !== null) {
                TableauFrais.update(vm.tableauFrais, onSaveSuccess, onSaveError);
            } else {
                TableauFrais.save(vm.tableauFrais, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:tableauFraisUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDebut = false;
        vm.datePickerOpenStatus.dateFin = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

    }
})();