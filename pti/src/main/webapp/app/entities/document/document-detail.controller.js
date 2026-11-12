(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DocumentDetailController', DocumentDetailController);

    DocumentDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'Document', 'TypeDocument'];

    function DocumentDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, Document, TypeDocument) {
        var vm = this;

        vm.document = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('ptiApp:documentUpdate', function(event, result) {
            vm.document = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
