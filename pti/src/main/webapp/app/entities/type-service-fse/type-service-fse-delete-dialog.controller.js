(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceFseDeleteController',TypeServiceFseDeleteController);

        TypeServiceFseDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeServiceFse'];

    function TypeServiceFseDeleteController($uibModalInstance, entity, TypeServiceFse) {
        var vm = this;

        vm.typeServiceFse = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            TypeServiceFse.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
