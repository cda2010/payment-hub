(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('RelationDetailController', RelationDetailController);

    RelationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Relation'];

    function RelationDetailController($scope, $rootScope, $stateParams, previousState, entity, Relation) {
        var vm = this;

        vm.relation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:relationUpdate', function(event, result) {
            vm.relation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
