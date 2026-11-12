(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChampsDeleteController',ChampsDeleteController);

    ChampsDeleteController.$inject = ['$uibModalInstance', 'entity', 'Champs'];

    function ChampsDeleteController($uibModalInstance, entity, Champs) {
        var vm = this;

        vm.champs = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Champs.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
