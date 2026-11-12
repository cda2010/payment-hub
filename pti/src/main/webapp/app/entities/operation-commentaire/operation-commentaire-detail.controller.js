(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationCommentaireDetailController', OperationCommentaireDetailController);

    OperationCommentaireDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'OperationCommentaire', 'Operation'];

    function OperationCommentaireDetailController($scope, $rootScope, $stateParams, previousState, entity, OperationCommentaire, Operation) {
        var vm = this;

        vm.operationCommentaire = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:operationCommentaireUpdate', function(event, result) {
            vm.operationCommentaire = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
