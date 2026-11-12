(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ParametreDetailController', ParametreDetailController);

    ParametreDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Parametre'];

    function ParametreDetailController($scope, $rootScope, $stateParams, previousState, entity, Parametre) {
        var vm = this;

        vm.parametre = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:parametreUpdate', function(event, result) {
            vm.parametre = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
