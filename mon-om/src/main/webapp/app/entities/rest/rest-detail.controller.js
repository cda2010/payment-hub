(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('RestDetailController', RestDetailController);

    RestDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Rest'];

    function RestDetailController($scope, $rootScope, $stateParams, previousState, entity, Rest) {
        var vm = this;

        vm.rest = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:restUpdate', function(event, result) {
            vm.rest = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
