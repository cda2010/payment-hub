(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BlackListDeleteController',BlackListDeleteController);

    BlackListDeleteController.$inject = ['$uibModalInstance', 'entity', 'BlackList'];

    function BlackListDeleteController($uibModalInstance, entity, BlackList) {
        var vm = this;

        vm.blackList = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            BlackList.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
