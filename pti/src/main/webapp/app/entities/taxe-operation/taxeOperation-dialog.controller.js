(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeOperationDialogController', TaxeOperationDialogController);

    TaxeOperationDialogController.$inject = ['RequestUtils', 'DateUtils', 'Taxe', '$rootScope', 'ChoisirUo', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TaxeOperation', 'UniteOrganisation', 'TypeService', 'Operation'];

    function TaxeOperationDialogController(RequestUtils, DateUtils, Taxe, $rootScope, ChoisirUo, $timeout, $scope, $stateParams, $uibModalInstance, entity, TaxeOperation, UniteOrganisation, TypeService, Operation) {
        var vm = this;

        vm.taxeOperation = entity;
        vm.taxes = Taxe.query();
        vm.clear = clear;
        vm.save = save;
        vm.taxeOperation.appliqueSur="FRAIS";
        vm.uniteorganisations = UniteOrganisation.query();
        TypeService.queryAll({}, function (datas) {
            vm.taxeOperation.typeServiceIds = [];
            vm.typeservices=datas;
            if (angular.isDefined(vm.taxeOperation) && vm.taxeOperation != null && vm.taxeOperation.id != null) {
                vm.organisation.libelleUniteOrganisation = vm.taxeOperation.libelleUniteOrganisation;
                vm.taxeOperation.dateDebut = DateUtils.convertLocalDateFromServer(vm.taxeOperation.dateDebut);
                vm.taxeOperation.dateFin = DateUtils.convertLocalDateFromServer(vm.taxeOperation.dateFin);
                angular.forEach(vm.taxeOperation.typeServices, function (value, index) {
                    vm.taxeOperation.typeServiceIds.push(value.id);
                });
                console.log(vm.taxeOperation.typeServiceIds);
            }
        });
        //vm.operations = Operation.query();
        vm.operations = null;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus.dateDebut = false;
        vm.datePickerOpenStatus.dateFin = false;
        vm.organisation = {};
       
        vm.taxeOperation.appliqueSur = "FRAIS";
        
        RequestUtils.GetOrganisationByType.get({ type: "ZONE" }, function (datas) {
            vm.zones = datas;
        });

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }
        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        vm.choisirUo = function () {
            var data = {
                "acteurs": ["PAYS", "AGENCE", "MASTER", "SOUS_DISTRIBUTEUR"]
            };
            ChoisirUo.open($scope, data);
        }

        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            if (result != null) {
                vm.organisation = result;
                vm.taxeOperation.uniteOrganisationId = result.id;
            }
        });

        vm.serviceColumns = [{
            name: 'libelle',
            title: 'Service'
        }];

        function save() {
            console.log('vm.taxeOperation',vm.taxeOperation);
            vm.isSaving = true;
            if (vm.taxeOperation.id !== null) {
                TaxeOperation.update(vm.taxeOperation, onSaveSuccess, onSaveError);
            } else {
                TaxeOperation.save(vm.taxeOperation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:taxeOperationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        $scope.$watch("vm.taxeOperation.typeServiceIds", function (items) {
            if (angular.isDefined(items) && items != null) {
                vm.taxeOperation.typeServices = [];
                angular.forEach(items, function (value, index) {
                    var s = _.find(vm.typeservices, { 'id': value });
                    vm.taxeOperation.typeServices.push(s);
                });
                console.log(vm.taxeOperation.typeServices);
            }
        });



    }
})();
