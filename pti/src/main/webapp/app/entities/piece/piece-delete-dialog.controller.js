(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PieceDeleteController',PieceDeleteController);

    PieceDeleteController.$inject = ['$uibModalInstance', 'entity', 'Piece'];

    function PieceDeleteController($uibModalInstance, entity, Piece) {
        var vm = this;

        vm.piece = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Piece.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
