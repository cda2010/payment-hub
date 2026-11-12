(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeDocumentDialogController', TypeDocumentDialogController);

    TypeDocumentDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeDocument'];

    function TypeDocumentDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypeDocument) {
        var vm = this;

        vm.typeDocument = entity;
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
            if (vm.typeDocument.id !== null) {
                TypeDocument.update(vm.typeDocument, onSaveSuccess, onSaveError);
            } else {
                TypeDocument.save(vm.typeDocument, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:typeDocumentUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
