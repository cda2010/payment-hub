(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('AutreParametreDeleteController',AutreParametreDeleteController);

    AutreParametreDeleteController.$inject = ['$uibModalInstance', 'entity', 'AutreParametre'];

    function AutreParametreDeleteController($uibModalInstance, entity, AutreParametre) {
        var vm = this;

        vm.autreParametre = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            AutreParametre.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
