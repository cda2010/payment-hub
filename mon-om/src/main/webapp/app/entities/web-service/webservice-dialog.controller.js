(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('WebserviceDialogController', WebserviceDialogController);

    WebserviceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Webservice'];

    function WebserviceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Webservice) {
        var vm = this;

        vm.webservice = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.webservice.id !== null) {
                Webservice.update(vm.webservice, onSaveSuccess, onSaveError);
            } else {
                Webservice.save(vm.webservice, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:webserviceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
