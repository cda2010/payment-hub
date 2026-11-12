(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionFraisDialogController', RepartitionFraisDialogController);

    RepartitionFraisDialogController.$inject = ['DateUtils','RequestUtils','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RepartitionFrais', 'TypeService'];

    function RepartitionFraisDialogController (DateUtils,RequestUtils,$timeout, $scope, $stateParams, $uibModalInstance, entity, RepartitionFrais, TypeService) {
        var vm = this;

        vm.repartitionFrais = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typeservices = TypeService.queryAllOptimise();
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.relationChange=function(){
            vm.repartitionFrais.fseSourceId=vm.relation.fseSource.id;
            vm.repartitionFrais.fseDestId=vm.relation.fseDestination.id;
        };
        RequestUtils.GetFournisseurRelation.query({}, function (datas) {
           vm.relations=datas;
        }, function () {
          
        });

        if (vm.repartitionFrais.id != null) {
            vm.repartitionFrais.dateDebut = DateUtils.convertLocalDateFromServer(vm.repartitionFrais.dateDebut);
            vm.repartitionFrais.dateFin = DateUtils.convertLocalDateFromServer(vm.repartitionFrais.dateFin);
        }

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.repartitionFrais.id !== null) {
                RepartitionFrais.update(vm.repartitionFrais, onSaveSuccess, onSaveError);
            } else {
                RepartitionFrais.save(vm.repartitionFrais, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:repartitionFraisUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDebut = false;
        vm.datePickerOpenStatus.dateFin = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

    }
})();
