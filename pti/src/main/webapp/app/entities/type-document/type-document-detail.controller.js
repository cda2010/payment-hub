(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeDocumentDetailController', TypeDocumentDetailController);

    TypeDocumentDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeDocument'];

    function TypeDocumentDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeDocument) {
        var vm = this;

        vm.typeDocument = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typeDocumentUpdate', function(event, result) {
            vm.typeDocument = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
