(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LienDeleteController',LienDeleteController);

    LienDeleteController.$inject = ['$uibModalInstance', 'entity', 'Lien'];

    function LienDeleteController($uibModalInstance, entity, Lien) {
        var vm = this;

        vm.lien = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Lien.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
