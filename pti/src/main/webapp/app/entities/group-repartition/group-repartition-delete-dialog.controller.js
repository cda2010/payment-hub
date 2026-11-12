(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupRepartitionDeleteController',GroupRepartitionDeleteController);

    GroupRepartitionDeleteController.$inject = ['$uibModalInstance', 'entity', 'GroupRepartition'];

    function GroupRepartitionDeleteController($uibModalInstance, entity, GroupRepartition) {
        var vm = this;

        vm.groupRepartition = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            GroupRepartition.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
