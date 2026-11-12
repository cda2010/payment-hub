(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeDocumentDeleteController',TypeDocumentDeleteController);

    TypeDocumentDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeDocument'];

    function TypeDocumentDeleteController($uibModalInstance, entity, TypeDocument) {
        var vm = this;

        vm.typeDocument = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypeDocument.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
