(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypePieceDeleteController',TypePieceDeleteController);

    TypePieceDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypePiece'];

    function TypePieceDeleteController($uibModalInstance, entity, TypePiece) {
        var vm = this;

        vm.typePiece = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypePiece.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
