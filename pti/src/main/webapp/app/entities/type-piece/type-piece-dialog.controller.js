(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypePieceDialogController', TypePieceDialogController);

    TypePieceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypePiece', 'Piece'];

    function TypePieceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypePiece, Piece) {
        var vm = this;

        vm.typePiece = entity;
        vm.clear = clear;
        vm.save = save;
        vm.pieces = Piece.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.typePiece.id !== null) {
                TypePiece.update(vm.typePiece, onSaveSuccess, onSaveError);
            } else {
                TypePiece.save(vm.typePiece, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:typePieceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
