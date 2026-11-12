(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EcranDeleteController',EcranDeleteController);

    EcranDeleteController.$inject = ['$uibModalInstance', 'entity', 'Ecran'];

    function EcranDeleteController($uibModalInstance, entity, Ecran) {
        var vm = this;

        vm.ecran = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Ecran.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
