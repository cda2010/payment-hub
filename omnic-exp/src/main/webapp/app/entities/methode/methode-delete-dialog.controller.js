(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('MethodeDeleteController',MethodeDeleteController);

    MethodeDeleteController.$inject = ['$uibModalInstance', 'entity', 'Methode'];

    function MethodeDeleteController($uibModalInstance, entity, Methode) {
        var vm = this;

        vm.methode = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Methode.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
