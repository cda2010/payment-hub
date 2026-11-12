(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AutorisationDeleteController',AutorisationDeleteController);

    AutorisationDeleteController.$inject = ['$uibModalInstance', 'entity', 'Autorisation'];

    function AutorisationDeleteController($uibModalInstance, entity, Autorisation) {
        var vm = this;

        vm.autorisation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Autorisation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
