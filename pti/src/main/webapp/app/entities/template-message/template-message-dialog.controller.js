(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TemplateMessageDialogController', TemplateMessageDialogController);

    TemplateMessageDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TemplateMessage','UniteOrganisation','TypeService'];

    function TemplateMessageDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TemplateMessage,UniteOrganisation,TypeService) {
        var vm = this;

        vm.templateMessage = entity;
        vm.clear = clear;
        vm.save = save;
        vm.FSEs=[];
        vm.typeServices=[];
        TypeService.queryAll(function (result) {
            vm.typeServices=result;
        })
        UniteOrganisation.queryByTypeUoFSE(function (result) {
            vm.FSEs=result;
        });

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.templateMessage.id !== null) {
                TemplateMessage.update(vm.templateMessage, onSaveSuccess, onSaveError);
            } else {
                TemplateMessage.save(vm.templateMessage, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:templateMessageUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
