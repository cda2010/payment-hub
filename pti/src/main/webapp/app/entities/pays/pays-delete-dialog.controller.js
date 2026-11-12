(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PaysDeleteController',PaysDeleteController);

    PaysDeleteController.$inject = ['$uibModalInstance', 'entity', 'Pays'];

    function PaysDeleteController($uibModalInstance, entity, Pays) {
        var vm = this;

        vm.pays = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Pays.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
