(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauRepartitionCommissionDialogController', TableauRepartitionCommissionDialogController);

    TableauRepartitionCommissionDialogController.$inject = ['$rootScope','ChoisirUo','TypeUOWithoutUR','DateUtils', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TableauRepartitionCommission', 'LigneRepartitionCommission', 'TypeService','GroupRepartition'];

    function TableauRepartitionCommissionDialogController($rootScope,ChoisirUo,TypeUOWithoutUR,DateUtils, $timeout, $scope, $stateParams, $uibModalInstance, entity, TableauRepartitionCommission, LigneRepartitionCommission, TypeService,GroupRepartition) {
        var vm = this;

        vm.tableauRepartitionCommission = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        //vm.lignerepartitioncommissions = LigneRepartitionCommission.query();
        vm.typeservices = TypeService.queryAll();
        vm.groups=GroupRepartition.query();
        vm.setTypeService=function(){
            GroupRepartition.get({id:vm.tableauRepartitionCommission.groupId},function (result) {
                vm.tableauRepartitionCommission.typeServiceId=result.typeServiceId;
            })
        }

        TypeUOWithoutUR.optimise({}, function (datas) {
            removeItem(datas,"libelleTypeUO","FSE");
            vm.typeUos = datas;
        });

        $timeout(function() {
            angular.element('.form-group:eq(1)>input').focus();
        });

        vm.organisation={};

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        if (vm.tableauRepartitionCommission.id != null) {
            vm.tableauRepartitionCommission.dateDebut = DateUtils.convertLocalDateFromServer(vm.tableauRepartitionCommission.dateDebut);
            vm.tableauRepartitionCommission.dateFin = DateUtils.convertLocalDateFromServer(vm.tableauRepartitionCommission.dateFin);
            vm.organisation.libelleUniteOrganisation=vm.tableauRepartitionCommission.organisationLibelle;
        }

        vm.choisirUo = function () {
            ChoisirUo.open($scope);
        }


        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.tableauRepartitionCommission.organisationId=result.id;
            }
        });


        function save() {
            vm.isSaving = true;
            if (vm.tableauRepartitionCommission.id !== null) {
                TableauRepartitionCommission.update(vm.tableauRepartitionCommission, onSaveSuccess, onSaveError);
            } else {
                TableauRepartitionCommission.save(vm.tableauRepartitionCommission, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:tableauRepartitionCommissionUpdate', result);
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

        //fonction pour supprimer un element d'un tableau
        var removeItem = function (object, key, value) {
            if (value == undefined)
                return;
            for (var i in object) {
                if (object[i][key] == value) {
                    object.splice(i, 1);
                }
            }
        };
    }
})();
