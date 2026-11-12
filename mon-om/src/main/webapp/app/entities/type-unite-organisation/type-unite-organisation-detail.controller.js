(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeUniteOrganisationDetailController', TypeUniteOrganisationDetailController);

    TypeUniteOrganisationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeUniteOrganisation'];

    function TypeUniteOrganisationDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeUniteOrganisation) {
        var vm = this;

        vm.typeUniteOrganisation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:typeUniteOrganisationUpdate', function(event, result) {
            vm.typeUniteOrganisation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
