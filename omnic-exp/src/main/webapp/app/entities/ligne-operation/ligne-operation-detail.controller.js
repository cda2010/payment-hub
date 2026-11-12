(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneOperationDetailController', LigneOperationDetailController);

    LigneOperationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneOperation', 'Operation'];

    function LigneOperationDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneOperation, Operation) {
        var vm = this;

        vm.ligneOperation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:ligneOperationUpdate', function(event, result) {
            vm.ligneOperation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
