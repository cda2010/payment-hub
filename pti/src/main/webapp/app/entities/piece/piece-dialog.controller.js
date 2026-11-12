(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PieceDialogController', PieceDialogController);

    PieceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Piece', 'Personne', 'TypePiece'];

    function PieceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Piece, Personne, TypePiece) {
        var vm = this;

        vm.piece = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.personnes = Personne.query();
        vm.typepieces = TypePiece.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.piece.id !== null) {
                Piece.update(vm.piece, onSaveSuccess, onSaveError);
            } else {
                Piece.save(vm.piece, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:pieceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateDelivrance = false;
        vm.datePickerOpenStatus.dateExpiration = false;

        vm.setCopie = function ($file, piece) {
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        piece.copie = base64Data;
                        piece.copieContentType = $file.type;
                    });
                });
            }
        };

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
