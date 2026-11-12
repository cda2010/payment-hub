'use strict';

describe('Controller Tests', function() {

    describe('AutreParametre Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockAutreParametre, MockFournisseurService;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockAutreParametre = jasmine.createSpy('MockAutreParametre');
            MockFournisseurService = jasmine.createSpy('MockFournisseurService');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'AutreParametre': MockAutreParametre,
                'FournisseurService': MockFournisseurService
            };
            createController = function() {
                $injector.get('$controller')("AutreParametreDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:autreParametreUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
