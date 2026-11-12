(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreMethodeDeleteController',ParametreMethodeDeleteController);

    ParametreMethodeDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParametreMethode'];

    function ParametreMethodeDeleteController($uibModalInstance, entity, ParametreMethode) {
        var vm = this;

        vm.parametreMethode = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ParametreMethode.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
