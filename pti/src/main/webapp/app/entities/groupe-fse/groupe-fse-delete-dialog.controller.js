(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeFseDeleteController',GroupeFseDeleteController);

        GroupeFseDeleteController.$inject = ['$uibModalInstance', 'entity', 'GroupeFse'];

    function GroupeFseDeleteController($uibModalInstance, entity, GroupeFse) {
        var vm = this;

        vm.groupeFse = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            GroupeFse.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
