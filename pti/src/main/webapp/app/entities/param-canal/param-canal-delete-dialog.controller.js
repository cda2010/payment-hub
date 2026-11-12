(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamCanalDeleteController',ParamCanalDeleteController);

        ParamCanalDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParamCanal'];

    function ParamCanalDeleteController($uibModalInstance, entity, ParamCanal) {
        var vm = this;

        vm.paramCanal = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            console.log(id);
            ParamCanal.delete({id: id},
                   
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
